package com.schreiber.code.seamless.aperol.util;


import android.content.res.AssetManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class AssetPathLoader {

    private final List<String> paths = new ArrayList<>();

    public AssetPathLoader(AssetManager assets, String root) {
        initializePaths(assets, root);
    }

    public List<String> getPaths() {
        return paths;
    }

    private void initializePaths(AssetManager assets, String path) {
        try {
            String[] list = assets.list(path);
            boolean isFolder = list.length > 0;
            if (isFolder) {
                for (String file : list) {
                    initializePaths(assets, path + "/" + file);
                }
            } else {
                paths.add(path);
            }
        } catch (IOException e) {
            Logger.logException(e);
        }
    }
}
