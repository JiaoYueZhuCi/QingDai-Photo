export interface Photo {
    id: string;
    fileName: string;
    author: string;
    width: number;
    height: number;
    aperture: string;
    iso: string;
    shutter: string;
    camera: string;
    lens: string;
    time: string;
    title: string;
    description: string;
    location: string;
    tags: string[];
    originalSrc: string;
    compressedSrc: string;
    thumbnailSrc: string;
    groupId?: string;
} 