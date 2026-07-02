import pluginVue from 'eslint-plugin-vue'
import tsEslint from 'typescript-eslint'
import vueParser from 'vue-eslint-parser'

export default [
  // 忽略目录
  { ignores: ['dist/**', 'node_modules/**', '*.config.js'] },

  // 基础 TypeScript 规则
  ...tsEslint.configs.recommended,

  // Vue 规则（所有 .vue + .ts 文件）
  ...pluginVue.configs['flat/essential'],

  {
    files: ['**/*.vue'],
    languageOptions: {
      parser: vueParser,
      parserOptions: {
        parser: tsEslint.parser,
        ecmaVersion: 'latest',
        sourceType: 'module',
      },
    },
    rules: {
      'vue/multi-word-component-names': 'off',
      'vue/no-reserved-component-names': 'off',
    },
  },

  {
    files: ['**/*.ts'],
    languageOptions: {
      parser: tsEslint.parser,
      parserOptions: {
        ecmaVersion: 'latest',
        sourceType: 'module',
      },
    },
  },

  // 自定义规则
  {
    rules: {
      '@typescript-eslint/no-explicit-any': 'warn',
      '@typescript-eslint/no-unused-vars': ['warn', { argsIgnorePattern: '^_' }],
      'no-console': ['warn', { allow: ['warn', 'error'] }],
      'no-debugger': 'error',
    },
  },
]
