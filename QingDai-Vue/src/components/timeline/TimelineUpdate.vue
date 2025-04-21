<template>
    <el-dialog v-model="visible" title="添加时间轴" width="600px" :before-close="handleClose">
        <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
            <el-form-item label="时间" prop="time">
                <el-date-picker 
                    v-model="form.time" 
                    type="daterange" 
                    range-separator="至"
                    start-placeholder="开始日期"
                    end-placeholder="结束日期"
                    format="YYYY/MM/DD"
                    value-format="YYYY/MM/DD"
                    style="width: 100%"
                />
            </el-form-item>
            <el-form-item label="标题" prop="title">
                <el-input v-model="form.title" placeholder="请输入标题"></el-input>
            </el-form-item>
            <el-form-item label="内容" prop="text">
                <el-input v-model="form.text" type="textarea" :rows="5" placeholder="请输入内容"></el-input>
            </el-form-item>

            <el-form-item>
                <el-button type="primary" @click="submitForm">提交</el-button>
                <el-button @click="resetForm">重置</el-button>
            </el-form-item>
        </el-form>

        <!-- 卡片预览 -->
        <div class="preview-container">
            <h3>预览效果</h3>
            <el-timeline>
                <el-timeline-item :timestamp="form.time ? `${form.time[0]} 至 ${form.time[1]}` : '日期未选择'" placement="top">
                    <el-card>
                        <h4>{{ form.title || '标题未填写' }}</h4>
                        <p>{{ form.text || '内容未填写' }}</p>
                    </el-card>
                </el-timeline-item>
            </el-timeline>
        </div>
    </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, defineExpose, defineEmits, defineProps, watch } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { addTimeline } from '@/api/timeline'

// 定义props和emit用于支持v-model
const props = defineProps({
    modelValue: {
        type: Boolean,
        default: false
    }
});

const emit = defineEmits(['update:modelValue', 'timeline-added']);

// 本地的对话框可见性控制
const visible = ref(props.modelValue);

// 同步内外部的状态
watch(() => props.modelValue, (val) => {
    visible.value = val;
});

watch(visible, (val) => {
    emit('update:modelValue', val);
});

const formRef = ref<FormInstance>();

const form = reactive({
    time: [] as string[],
    title: '',
    text: ''
})

const rules = reactive<FormRules>({
    time: [
        { required: true, message: '请选择时间范围', trigger: 'change' }
    ],
    title: [
        { required: true, message: '请输入标题', trigger: 'blur' },
        { min: 1, max: 100, message: '长度在 1 到 100 个字符', trigger: 'blur' }
    ],
    text: [
        { required: true, message: '请输入内容', trigger: 'blur' }
    ]
})

const submitForm = async () => {
    if (!formRef.value) return
    
    await formRef.value.validate(async (valid, fields) => {
        if (valid) {
            try {
                await addTimeline({
                    time: `${form.time[0]} - ${form.time[1]}`,
                    title: form.title,
                    text: form.text
                })
                
                ElMessage.success('添加成功')
                resetForm()
                visible.value = false
                emit('timeline-added')
            } catch (error) {
                console.error('添加失败:', error)
                ElMessage.error('添加失败')
            }
        } else {
            console.log('验证失败:', fields)
        }
    })
}

const resetForm = () => {
    if (!formRef.value) return
    formRef.value.resetFields()
}

const handleClose = (done: () => void) => {
    resetForm()
    done()
}

defineExpose({
    visible
})
</script>

<style scoped>
.preview-container {
    padding: 15px;
    background-color: #f5f7fa;
    border-radius: 6px;
}

h3 {
    margin-top: 0;
    margin-bottom: 15px;
    color: #606266;
}
</style> 