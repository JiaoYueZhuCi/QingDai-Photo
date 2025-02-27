<template>
  <div class="image-row" v-for="(row, rowIndex) in rows" :key="rowIndex">
    <div 
      class="image-item" 
      v-for="(item, index) in row" 
      :key="item.id"
      :class="{ 'selected': isSelected(rowIndex, item.id) }"
      @click="selectItem(rowIndex, item.id)"
    >
      <img :src="item.url" />
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';

interface ImageItem {
  id: string;
  url: string;
}

export default defineComponent({
  data() {
    return {
      selectedRow: -1,
      selectedId: ''
    };
  },
  methods: {
    selectItem(rowIndex: number, itemId: string) {
      this.selectedRow = rowIndex;
      this.selectedId = itemId;
    },
    isSelected(rowIndex: number, itemId: string): boolean {
      return this.selectedRow === rowIndex && this.selectedId === itemId;
    }
  }
});
</script>