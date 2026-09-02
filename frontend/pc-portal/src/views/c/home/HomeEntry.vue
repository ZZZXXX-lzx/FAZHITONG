<template>
  <component :is="isMobile ? MobileHome : HomePage" />
</template>

<script setup>
import { computed } from 'vue'
import HomePage from './HomePage.vue'
import MobileHome from '../mobile/MobileHome.vue'
import { isMobileDevice } from '@/utils/device'

const isMobile = computed(() => isMobileDevice())
</script>
