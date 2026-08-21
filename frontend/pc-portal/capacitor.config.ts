import type { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.fabaotong.app',
  appName: '法保通',
  webDir: 'dist',
  server: {
    cleartext: true,
    allowNavigation: ['10.80.46.126', 'localhost', '192.168.*'],
  }
};

export default config;
