/**
 * 系统信息数据文件
 * 在此存储系统相关的静态配置信息
 */

// 系统启动时间
export const SYSTEM_START_DATE = '2025-02-25';

// 后端启动时间（由后端API返回后保存）
let backendStartTime: string | null = null;

// 前端部署时间（通过API获取）
let frontendDeployTime: string | null = null;

/**
 * 初始化前端部署时间
 * 从Nginx配置的/frontend-time接口获取
 */
export const initFrontendDeployTime = async (): Promise<void> => {
    try {
        const response = await fetch('/frontend-time');
        if (response.ok) {
            const timestamp = await response.text(); // 获取原始时间戳字符串
            const timestampMs = parseInt(timestamp) * 1000; // 秒转毫秒[1,6](@ref)
            
            // 原生 Date 对象转换（兼容性强）
            const date = new Date(timestampMs);
            frontendDeployTime = `${date.getFullYear()}-${(date.getMonth()+1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}:${date.getSeconds().toString().padStart(2, '0')}`;

        } else {
            console.error('获取前端部署时间失败:', response.statusText);
            frontendDeployTime = new Date().toISOString(); // 降级方案
        }
    } catch (e) {
        console.error('获取前端部署时间出错:', e);
        frontendDeployTime = new Date().toISOString(); // 降级方案
    }
};

/**
 * 设置后端启动时间
 * @param startTime ISO格式的启动时间字符串
 */
export const setBackendStartTime = (startTime: string) => {
    backendStartTime = startTime;
};

/**
 * 获取系统已运行时间（精确到天时分秒）
 * @returns 格式化的时间字符串
 */
export const getSystemRunningDays = (): string => {
    const startDate = new Date(SYSTEM_START_DATE);
    const currentDate = new Date();

    return formatTimeDifference(startDate, currentDate);
};

/**
 * 获取前端部署时间（精确到天时分秒）
 * @returns 格式化的时间字符串
 */
export const getFrontendDeploymentDays = (): string => {
    // 如果前端部署时间还未获取到，显示加载中
    if (!frontendDeployTime) {
        return '加载中...';
    }

    // 使用从Nginx获取的前端部署时间
    const deployTime = new Date(frontendDeployTime);
    const currentTime = new Date();

    return formatTimeDifference(deployTime, currentTime);
};

/**
 * 获取后端运行时间（精确到天时分秒）
 * @returns 格式化的时间字符串
 */
export const getBackendRunningTime = (): string => {
    if (!backendStartTime) {
        return '等待获取...';
    }

    const startTime = new Date(backendStartTime);
    const currentTime = new Date();

    return formatTimeDifference(startTime, currentTime);
};

/**
 * 格式化时间差为x天x小时x分钟x秒的格式
 * @param startDate 开始时间
 * @param endDate 结束时间
 * @returns 格式化后的时间差
 */
export const formatTimeDifference = (startDate: Date, endDate: Date): string => {
    // 计算时间差（毫秒）
    const timeDiff = Math.max(0, endDate.getTime() - startDate.getTime());

    // 计算天数、小时、分钟和秒
    const seconds = Math.floor(timeDiff / 1000) % 60;
    const minutes = Math.floor(timeDiff / (1000 * 60)) % 60;
    const hours = Math.floor(timeDiff / (1000 * 60 * 60)) % 24;
    const days = Math.floor(timeDiff / (1000 * 60 * 60 * 24));

    return `${days}天${hours}小时${minutes}分钟${seconds}秒`;
}; 