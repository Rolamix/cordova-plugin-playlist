const path = require('path');
const fs = require('fs-extra');
const { getAndroidJavaSrcPath, getPackageName, getProjectName } = require('./utils');

// this script needs to delete the MainApplication file that was added.

module.exports = function androidAfterPluginRemove(context) {
  const deferral = context.requireCordovaModule('q').defer();

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
    deferral.resolve();
  } catch (ex) {
    console.warn(ex);
    deferral.reject(ex);
  }
};
