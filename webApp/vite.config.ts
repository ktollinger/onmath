import { defineConfig, loadEnv, type Plugin } from 'vite';
import vue from '@vitejs/plugin-vue';
import { VitePWA } from 'vite-plugin-pwa';

function buildTimestampPlugin(): Plugin {
  return {
    name: 'onmath-build-timestamp',
    transformIndexHtml(html) {
      const now = new Date();
      const injection = `    <meta name="build-time" content="${now.toISOString()}" />\n    <!-- Build timestamp: ${now.toUTCString()} -->\n`;
      return html.replace('</head>', `${injection}  </head>`);
    },
  };
}

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, '.', '');
  const base = env.VITE_BASE_PATH || '/';

  return {
    base,
    plugins: [
      vue(),
      buildTimestampPlugin(),
      VitePWA({
        registerType: 'autoUpdate',
        includeAssets: ['icon.svg', 'apple-touch-icon.png'],
        manifest: {
          name: 'OnMath',
          short_name: 'OnMath',
          description: 'Offline 4th-grade math practice: multiplication, expressions, and column subtraction.',
          theme_color: '#1E6BB8',
          background_color: '#F8FAFD',
          display: 'standalone',
          orientation: 'portrait',
          start_url: './',
          scope: './',
          icons: [
            {
              src: 'pwa-512x512.png',
              sizes: '512x512',
              type: 'image/png',
              purpose: 'any maskable',
            },
            {
              src: 'icon.svg',
              sizes: 'any',
              type: 'image/svg+xml',
            },
          ],
        },
      }),
    ],
  };
});
