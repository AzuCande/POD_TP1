package ar.edu.itba.pod.models;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import ar.edu.itba.pod.models.exceptions.planeExceptions.IllegalPlaneException;

public class PlaneModel implements Serializable {
    private final String model;

    private final Map<RowCategory, int[]> rowCategoryMap;

    public PlaneModel(String model, Map<String, int[]> rowCategoryMap) {
        validateParams(rowCategoryMap);
        this.model = model;
        this.rowCategoryMap = rowCategoryMap.entrySet().stream()
                .collect(Collectors.toMap(e -> RowCategory.valueOf(e.getKey()), Map.Entry::getValue));
    }
    
    private void validateParams(Map<String, int[]> rowCategoryMap) {
        boolean hasValidRow = false;
        for (Map.Entry<String, int[]> entry : rowCategoryMap.entrySet()) {
            int[] val = entry.getValue();
            if (val[0] < 0 || val[1] < 0 )
                throw new IllegalPlaneException();

            if (val[0] > 0 && val[1] > 0)
                hasValidRow = true;
        }

        if(!hasValidRow)
            throw new IllegalPlaneException();
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
