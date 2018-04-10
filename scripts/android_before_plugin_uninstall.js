const path = require('path');
const fs = require('fs-extra');
const { getAndroidJavaSrcPath, getPackageName, getProjectName } = require('./utils');

const pluginPackage = 'com.rolamix.plugins.audioplayer';
const pluginInstalledPackagePath = pluginPackage.replace(/\./g, path.sep);

// this script needs to put back the MainApplication file that was moved.
// Cordova needs to be able to remove the file, unfortunately. It seems to keep track
// of files that were copied and even if the source file no longer exists, it marks
// the path as occupied and refuses to allow you to reinstall the plugin.

module.exports = function androidAfterPluginRemove(context) {
  const deferral = context.requireCordovaModule('q').defer();

  try {
    const projectName = getProjectName(context);
    const packageName = getPackageName(context);
    const javaTargetPath = getAndroidJavaSrcPath(context);
    const installedPluginPath = path.resolve(javaTargetPath, pluginInstalledPackagePath);

    const packagePath = packageName.replace(/\./g, path.sep);
    const mainAppTarget = path.resolve(javaTargetPath, packagePath, `${projectName}.java`);
    const pluginSource = path.resolve(installedPluginPath, 'MainApplication.java');

    console.log('Restoring generated MainApplication file to plugin directory: ', mainAppTarget, '\n', pluginSource);

    if (fs.existsSync(mainAppTarget)) {
      fs.unlinkSync(mainAppTarget);
    }
    deferral.resolve();
  } catch (ex) {
    console.warn(ex);
    deferral.reject(ex);
  }
};
