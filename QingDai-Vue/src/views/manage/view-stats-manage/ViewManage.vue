<template>
    <div class="view-manage">
        <ViewStatsBar />
        <ViewStatsCard />
    </div>
</template>

<script setup lang="ts">
import ViewStatsBar from '@/views/manage/develop-manage/view-stats-bar/ViewStatsBar.vue';
import ViewStatsCard from './view-stats-card/ViewStatsCard.vue';
</script>

<style scoped>
.view-manage {
    padding: 20px;
    background-color: var(--qd-color-bg-dark);
    height: 100%;
}
</style>
