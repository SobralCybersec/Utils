package com.dev.model;

import java.util.Arrays;
import java.util.List;

public enum FileType {
    IMAGES(Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "raw", "webp", "svg", "ico", "tiff", "tif")),
    VIDEOS(Arrays.asList("mp4", "mkv", "avi", "mov", "wmv", "flv", "webm", "m4v", "mpg", "mpeg")),
    DOCUMENTS(Arrays.asList("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "rtf", "odt", "ods", "odp", "csv")),
    CODE(Arrays.asList("java", "py", "js", "html", "css", "cpp", "c", "h", "hpp", "cs", "php", "rb", "go", "rs", "ts", "jsx", "tsx", "vue", "json", "xml", "yaml", "yml", "sql", "sh", "bat", "ps1")),
    ARCHIVES(Arrays.asList("zip", "rar", "7z", "tar", "gz", "bz2", "xz", "iso", "dmg")),
    EXECUTABLES(Arrays.asList("exe", "msi", "dll", "so", "dylib", "app", "deb", "rpm")),
    NETWORK(Arrays.asList("pcap", "pcapng", "cap", "dmp")),
    AUDIO(Arrays.asList("mp3", "wav", "flac", "aac", "ogg", "wma", "m4a", "opus")),
    OTHERS(Arrays.asList());

    private final List<String> extensions;

    FileType(List<String> extensions) {
        this.extensions = extensions;
    }

    public List<String> getExtensions() {
        return extensions;
    }

    public static FileType fromExtension(String ext) {
        if (ext == null || ext.isEmpty()) return OTHERS;
        ext = ext.toLowerCase().trim();

        String finalExt = ext;
        return Arrays.stream(values())
                .filter(t -> t.getExtensions().contains(finalExt))
                .findFirst()
                .orElse(OTHERS);
    }
}