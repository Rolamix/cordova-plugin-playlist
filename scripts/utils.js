const path = require('path');
const fs = require('fs-extra');
const semver = require('semver');
const xml2js = require('xml2js');

const cdv7PathSegments = ['app', 'src', 'main', 'java'];

function readXml(filePath) {
  let parsedData = '';
  try {
    const xmlData = fs.readFileSync(filePath);
    const xmlParser = new xml2js.Parser();
    xmlParser.parseString(xmlData, (err, data) => {
      if (!err && data) {
        parsedData = data;
      }
    });
  } catch (err) {
    throw new Error(`AudioPlayer plugin: failed to read file: ${filePath}`);
  }

  return parsedData;
}

function writeXml(filePath, content) {
  const xmlBuilder = new xml2js.Builder();
  const changedXmlData = xmlBuilder.buildObject(content);
  let isSaved = false;

  try {
    fs.writeFileSync(filePath, changedXmlData);
    isSaved = true;
  } catch (err) {
    throw new Error(`AudioPlayer plugin: failed to write file: ${filePath}`);
  }

  return isSaved;
}

function getProjectName(context) { // eslint-disable-line no-unused-vars
  // I'd like to use this, but there are some issues around this that need to be addressed.
  // const ConfigParser = require('cordova-lib').configparser;
  // const ConfigParser = context.requireCordovaModule('cordova-lib').configparser;
  // const { projectRoot } = context.opts;
  // const configFile = getProjectConfig(projectRoot);
  // const configXml = new ConfigParser(configFile);
  // const projectName = configXml.name()
  //   ? configXml.name().replace(/[^\w.]/g, '_') + 'Application'
  //   : 'MainApplication';
  const projectName = 'MainApplication';
  return projectName;
}

// ~Duplicate of 'cordova-lib/src/cordova/util.js' but they do not expose that module..
function getProjectConfig(projectRoot) {
  const rootPath = path.join(projectRoot, 'config.xml');
  const wwwPath = path.join(projectRoot, 'www', 'config.xml');
  if (fs.existsSync(rootPath)) {
    return rootPath;
  } else if (fs.existsSync(wwwPath)) {
    return wwwPath;
  }
  return false;
}

function getAndroidVersion(context) {
  const { projectRoot } = context.opts;
  const platformsPromise = context.cordova.projectMetadata.getPlatforms(projectRoot);
  const platforms = platformsPromise.valueOf();

  if (platforms && platforms.length > 0) {
    const android = platforms.find((obj) => obj.name === 'android');
    if (!android) { throw new Error('Unable to find android platform in installed platforms'); }
    return semver.coerce(android.version || android.spec);
  }
  throw new Error('Unable to read android platform data from project root');
}

function getAndroidJavaSrcPath(context) {
  const { projectRoot } = context.opts;
  const targetSegments = cdv7PathSegments;
  const platformTarget = path.resolve(projectRoot, 'platforms', 'android');
  const javaTargetPath = path.resolve(platformTarget, ...targetSegments);
  return javaTargetPath;
}

function getPackageName(context) {
  // const ConfigParser = require('cordova-lib').configparser;
  const ConfigParser = context.requireCordovaModule('cordova-lib').configparser;

  const { projectRoot } = context.opts;
  const configFile = getProjectConfig(projectRoot);
  const configXml = new ConfigParser(configFile);
  const packageName = configXml.android_packageName() || configXml.packageName();
  return packageName;
}

function doCodeGen(source, target, packageName, projectName) {
  // console.log('Gen code from ', '\n\t', source, '\n', 'to', '\n\t', target);

  // only for this file, this script runs on prepare as well and the file may already have been moved.
  if (source.indexOf('MainApplication.java') >= 0) {
    if (!fs.existsSync(source)) { return; }
  }

  // Read in the template, insert the packageName and projectName, and write out to target
  const appJava = fs.readFileSync(source, 'utf8')
    .replace(/__PACKAGE_NAME__/g, packageName)
    .replace(/__PROJECT_NAME__/g, projectName);

  fs.writeFileSync(target, appJava);
}

function getAndroidManifest(context) {
  const { projectRoot } = context.opts;
  const platformTarget = path.resolve(projectRoot, 'platforms', 'android');
  const manifestPath = path.join(platformTarget, 'app', 'src', 'main', 'AndroidManifest.xml');
  const manifest = readXml(manifestPath);

  return {
    path: manifestPath,
    contents: manifest,
  };
}

function updateAndroidManifestApplication(context, value) {
  const manifest = getAndroidManifest(context);
  const appNode = manifest.contents.manifest.application[0]; // corresponds to /manifest/application/ xml path
  if (value && appNode.$['android:name']) {
    // eslint-disable-next-line
    const message = `AudioPlayerPlugin: Refusing to overwrite AndroidManifest application name (android:name), non-empty value: ${appNode.$['android:name']}`;
    console.warn(message);
  }
  if (value) {
    appNode.$['android:name'] = value;
  } else {
    delete appNode.$['android:name'];
  }

  writeXml(manifest.path, manifest.contents);
}

module.exports = {
  getAndroidManifest,
  updateAndroidManifestApplication,
  getProjectConfig,
  getAndroidVersion,
  getAndroidJavaSrcPath,
  getPackageName,
  getProjectName,
  doCodeGen,
};
