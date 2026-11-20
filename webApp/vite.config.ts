import { execSync } from 'node:child_process';
import { readFileSync } from 'node:fs';
import { defineConfig, loadEnv, type Plugin } from 'vite';
import vue from '@vitejs/plugin-vue';
import { VitePWA } from 'vite-plugin-pwa';

function resolveGitSha(envSha?: string): string {
  if (envSha && envSha.trim().length > 0) {
    return envSha.trim().slice(0, 7);
  }
  try {
    return execSync('git rev-parse --short HEAD', { encoding: 'utf8' }).trim();
  } catch {
    return 'local';
  }
}

function readPackageVersion(): string {
  try {
    const pkg = JSON.parse(readFileSync('./package.json', 'utf8')) as { version?: string };
    return pkg.version ?? '0.1.0';
  } catch {
    return '0.1.0';
  }
}

function buildTimestampPlugin(now: Date): Plugin {
  return {
    name: 'onmath-build-timestamp',
    transformIndexHtml(html) {
      const injection = `    <meta name="build-time" content="${now.toISOString()}" />\n    <!-- Build timestamp: ${now.toUTCString()} -->\n`;
      return html.replace('</head>', `${injection}  </head>`);
    },
  };
}

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, '.', '');
  const base = env.VITE_BASE_PATH || '/';
  const buildNow = new Date();
  const appVersion = readPackageVersion();
  const gitSha = resolveGitSha(env.GITHUB_SHA || process.env.GITHUB_SHA);

  return {
    base,
    define: {
      __APP_VERSION__: JSON.stringify(appVersion),
      __GIT_SHA__: JSON.stringify(gitSha),
      __BUILD_TIME__: JSON.stringify(buildNow.toISOString()),
    },
    plugins: [
      vue(),
      buildTimestampPlugin(buildNow),
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
