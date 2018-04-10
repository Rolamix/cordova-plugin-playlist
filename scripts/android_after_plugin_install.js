const path = require('path');
const fs = require('fs-extra');
const { getAndroidJavaSrcPath, getPackageName, getProjectName } = require('./utils');

const pluginPackage = 'com.rolamix.plugins.audioplayer';
const pluginInstalledPackagePath = pluginPackage.replace(/\./g, path.sep);

// these are *path segments* to be joined, relative to plugin root.
const filesToGenerate = [
  ['MainApplication.java'],
  ['AudioPlayerPlugin.java'],
  ['RmxAudioPlayer.java'],
  ['service', 'MediaService.java'],
  ['service', 'MediaImageProvider.java'],
];

function doCodeGen(source, target, packageName, projectName) {
  console.log('Gen code from ', '\n\t', source, '\n', 'to', '\n\t', target);

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

module.exports = function androidAfterPluginInstall(context) {
  const deferral = context.requireCordovaModule('q').defer();

  try {
    const projectName = getProjectName(context);
    const packageName = getPackageName(context);
    const javaTargetPath = getAndroidJavaSrcPath(context);
    const packagePath = packageName.replace(/\./g, path.sep);
    const installedPluginPath = path.resolve(javaTargetPath, pluginInstalledPackagePath);

    console.log('Gen code using:', `${packageName}, ${packagePath}, ${projectName}`);

    filesToGenerate.forEach((genFilePieces) => {
      const source = path.resolve(installedPluginPath, ...genFilePieces);
      const target = path.resolve(installedPluginPath, ...genFilePieces);
      doCodeGen(source, target, packageName, projectName);
    });

    // Now that we have codegen'd the MainApplication file, we need to move it.
    const mainAppSource = path.resolve(installedPluginPath, 'MainApplication.java');
    const mainAppTarget = path.resolve(javaTargetPath, packagePath, `${projectName}.java`);
    if (fs.existsSync(mainAppSource)) {
      console.log('Moving MainApplication.java from ', '\n\t', mainAppSource, '\n', 'to', '\n\t', mainAppTarget);

      if (fs.existsSync(mainAppTarget)) {
        let message = `MainApplication.java target already exists, refusing to overwrite: ${mainAppTarget}`;
        message += 'This is a permanent error so that you have the opportunity to resolve the conflict.';
        throw new Error(message);
      }
      fs.moveSync(mainAppSource, mainAppTarget, { overwrite: false });
    }

    deferral.resolve();
  } catch (ex) {
    console.warn(ex);
    deferral.reject(ex);
  }

  return deferral.promise;
};
