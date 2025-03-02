<template>
  <router-view v-slot="{ Component }">
    <component :is="Component" />
  </router-view>
</template>

<script lang="ts">
import { useRouter, useRoute } from 'vue-router'
import { watch, ref, onMounted } from 'vue';



</script>
