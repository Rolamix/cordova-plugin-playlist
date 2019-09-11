const path = require('path');
const fs = require('fs-extra');
const q = require('q');
const {
  getAndroidJavaSrcPath, getPackageName, getProjectName, updateAndroidManifestApplication,
} = require('./utils');

// this script needs to put back the MainApplication file that was moved.
// Cordova needs to be able to remove the file, unfortunately. It seems to keep track
// of files that were copied and even if the source file no longer exists, it marks
// the path as occupied and refuses to allow you to reinstall the plugin.

module.exports = function androidAfterPluginRemove(context) {
  const deferral = q.defer();

  try {
    const projectName = getProjectName(context);
    const packageName = getPackageName(context);
    const javaTargetPath = getAndroidJavaSrcPath(context);

    const packagePath = packageName.replace(/\./g, path.sep);
    const mainAppTarget = path.resolve(javaTargetPath, packagePath, `${projectName}.java`);

    console.log('Removing generated MainApplication file: ', mainAppTarget);

    if (fs.existsSync(mainAppTarget)) {
      fs.unlinkSync(mainAppTarget);
    }

    // And remove the main application from the AndroidManifest.
    updateAndroidManifestApplication(context, null);

    deferral.resolve();
  } catch (ex) {
    console.warn(ex);
    deferral.reject(ex);
  }
};
