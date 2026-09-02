<template>
  <component :is="isMobile ? MobileLayout : MainLayout" />
</template>

<script setup>
import { computed } from 'vue'
import MainLayout from './MainLayout.vue'
import MobileLayout from './MobileLayout.vue'
import { isMobileDevice } from '@/utils/device'

const isMobile = computed(() => isMobileDevice())
</script>
