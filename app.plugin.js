const { withEntitlementsPlist } = require('@expo/config-plugins');

const withAppGroup = (config, { appGroup } = {}) => {
  return withEntitlementsPlist(config, (config) => {
    const entitlements = config.modResults;
    const key = 'com.apple.security.application-groups';
    
    if (appGroup) {
      const existing = entitlements[key] || [];
      if (!existing.includes(appGroup)) {
        entitlements[key] = [...existing, appGroup];
      }
    }
    
    return config;
  });
};

module.exports = withAppGroup;
