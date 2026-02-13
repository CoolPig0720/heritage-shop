<template>
  <div class="pagination">
    <el-pagination
      v-model:current-page="currentPageModel"
      v-model:page-size="pageSizeModel"
      :page-sizes="effectivePageSizes"
      :total="total"
      :layout="effectiveLayout"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  variant: {
    type: String,
    default: 'default'
  },
  currentPage: {
    type: Number,
    required: true
  },
  pageSize: {
    type: Number,
    required: true
  },
  total: {
    type: Number,
    required: true
  },
  pageSizes: {
    type: Array
  },
  layout: {
    type: String
  }
})

const emit = defineEmits(['update:currentPage', 'update:pageSize', 'size-change', 'current-change'])

const presets = {
  default: {
    pageSizes: [5, 10, 20, 50, 100],
    layout: 'total, sizes, prev, pager, next, jumper'
  },
  orders: {
    pageSizes: [5, 10, 20, 50],
    layout: 'total, prev, pager, next, sizes'
  },
  history: {
    pageSizes: [8, 12, 20, 40],
    layout: 'total, sizes, prev, pager, next'
  }
}

const preset = computed(() => presets[props.variant] || presets.default)

const effectivePageSizes = computed(() => {
  const val = props.pageSizes
  if (Array.isArray(val) && val.length > 0) return val
  return preset.value.pageSizes
})

const effectiveLayout = computed(() => {
  const val = props.layout
  if (typeof val === 'string' && val.trim()) return val
  return preset.value.layout
})

const currentPageModel = computed({
  get: () => props.currentPage,
  set: (val) => emit('update:currentPage', val)
})

const pageSizeModel = computed({
  get: () => props.pageSize,
  set: (val) => emit('update:pageSize', val)
})

const handleSizeChange = (val) => {
  emit('size-change', val)
}

const handleCurrentChange = (val) => {
  emit('current-change', val)
}
</script>
