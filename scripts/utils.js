const path = require('path');
const fs = require('fs-extra');
const semver = require('semver');

const cdv6PathSegments = ['src'];
const cdv7PathSegments = ['app', 'src', 'main', 'java'];

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
  const isCordovaAndroid7orMore = semver.satisfies(getAndroidVersion(context), '>=7.0.0');
  const targetSegments = isCordovaAndroid7orMore ? cdv7PathSegments : cdv6PathSegments;
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

module.exports = {
  getProjectConfig,
  getAndroidVersion,
  getAndroidJavaSrcPath,
  getPackageName,
  getProjectName,
};
