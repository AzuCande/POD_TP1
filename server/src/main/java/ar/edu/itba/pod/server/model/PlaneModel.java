package ar.edu.itba.pod.server.model;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class PlaneModel {
    private final String model;

    private final Map<RowCategory, int[]> rowCategoryMap;

    public PlaneModel(String model, Map<String, int[]> rowCategoryMap) {
        this.model = model;
        this.rowCategoryMap = rowCategoryMap.entrySet().stream()
                .collect(Collectors.toMap(e -> RowCategory.valueOf(e.getKey()), Map.Entry::getValue));
    }

    public int[] getCategoryConfig(RowCategory category) {
        return rowCategoryMap.getOrDefault(category, new int[]{0,0});
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlaneModel that = (PlaneModel) o;
        return model.equals(that.model);
    }

    @Override
    public int hashCode() {
        return Objects.hash(model);
    }

    public String getModel() {
        return model;
    }
}
