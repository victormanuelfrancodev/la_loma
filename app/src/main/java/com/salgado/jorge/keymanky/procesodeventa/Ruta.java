package com.salgado.jorge.keymanky.procesodeventa;

public class Ruta {
     int clienteId;
    String route;
    String onDate;
    String endDate;
    String checkinLocation;

    public Ruta(int clienteId, String route, String onDate, String endDate, String checkinLocation) {
        this.clienteId = clienteId;
        this.route = route;
        this.onDate = onDate;
        this.endDate = endDate;
        this.checkinLocation = checkinLocation;
    }

    public Ruta() {
        this.clienteId = clienteId;
        this.route = route;
        this.onDate = onDate;
        this.endDate = endDate;
        this.checkinLocation = checkinLocation;
    }

    public int getClienteId() {
        return clienteId;
    }

    public void setClienteId(int clienteId) {
        this.clienteId = clienteId;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public String getOnDate() {
        return onDate;
    }

    public void setOnDate(String onDate) {
        this.onDate = onDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getCheckinLocation() {
        return checkinLocation;
    }

    public void setCheckinLocation(String checkinLocation) {
        this.checkinLocation = checkinLocation;
    }
}
