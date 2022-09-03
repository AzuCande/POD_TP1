package ar.edu.itba.pod.server.model;

public class Plane {
    private final String model;
    private final Row[] rows;
    private final FlightState state = FlightState.PENDING; //TODO: esto creo que no va. es el modelo de un avión no un vuelo. eso es en flights

    public Plane(String model, int businessRows, int businessCols, int premEconomyRows, int premEconomyCols, int economyRows, int economyCols) {
        int totRows = businessRows + premEconomyRows + economyRows;
        if (validParams(businessRows, businessCols, premEconomyRows, premEconomyCols, economyRows, economyCols)) {
            throw new IllegalArgumentException(""); //TODO: crear nuestras excepciones
        }

        this.model = model;
        this.rows = new Row[totRows];
        int iter = 0;
        for (int i = 0; i < businessRows; iter++, i++) {
            rows[iter] = new Row(RowCategory.BUSINESS, businessCols);
        }

        for (int i = 0; i < premEconomyRows; iter++, i++) {
            rows[iter] = new Row(RowCategory.PREMIUM_ECONOMY, premEconomyCols);
        }

        for (int i = 0; i < businessRows; iter++, i++) {
            rows[iter] = new Row(RowCategory.ECONOMY, economyCols);
        }

    }

    public void assignSeat(int rowNumber, char seat, String passengerName) { //TODO: usar ticket y no passenger
        checkValidRow(rowNumber);
        if (state != FlightState.PENDING) {
            throw new IllegalStateException("Plane is not in pending state");
        }

        for (Row row : rows) {
            if (row.passengerHasSeat(passengerName)) {
                throw new IllegalStateException("Passenger already has a seat");
            }
        }

        rows[rowNumber].assignSeat(seat, passengerName);
    }

    public void changeSeat(int newRow, char newSeat, String passengerName) {//TODO: usar ticket y no passenger
        
        checkValidRow(newRow);
        if (state != FlightState.PENDING) {
            throw new IllegalStateException("Plane is not in pending state");
        }

        RowCategory prevRowCategory = null;
        for (Row row : rows) {
            if (row.passengerHasSeat(passengerName)) {
                row.removePassenger(passengerName);
                prevRowCategory = row.getRowCategory();
                break;
            }
        }

        if (prevRowCategory == null) {
            throw new IllegalStateException("Passenger does not have a seat");
        }

        // TODO: revisar, ticket tiene categoria
        if (prevRowCategory != rows[newRow].getRowCategory()) {
            throw new IllegalStateException("Passenger cannot change seat category");
        }

        rows[newRow].assignSeat(newSeat, passengerName);
    }

    private void checkValidRow(int row) {
        if (row < 0 || row >= rows.length) {
            throw new IllegalArgumentException("Row " + row + " does not exist");
        }

    }

    public String getModel() {
        return model;
    }

    //TODO: Revisar
    private boolean validParams(int businessRows, int businessCols, int premEconomyRows, int premEconomyCols, int economyRows, int economyCols) {
        return (businessRows + premEconomyRows + economyRows > 0) && (businessRows > 0 && businessCols > 0 || premEconomyRows > 0 && premEconomyCols > 0 || economyRows > 0 && economyCols > 0);
    }


}
