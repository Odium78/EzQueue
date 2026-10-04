/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Data;

/**
 *
 * @author lans
 */
public class QueueEntry {
    public enum Lane {
        REGULAR, PRIORITY
    }

    // regular senior pwd pregnat
    public enum Type {
        REGULAR("Regular", Lane.REGULAR),
        SENIOR("Senior", Lane.PRIORITY),
        PWD("PWD", Lane.PRIORITY),
        PREGNANT("Pregnant", Lane.PRIORITY);
 
        private final String label;
        private final Lane lane;
 
        Type(String label, Lane lane) {
            this.label = label;
            this.lane = lane;
        }
 
        public String getLabel() {
            return label;
        }
 
        public Lane getLane() {
            return lane;
        }
    }
 
    private final int number;
    private final Type type;
    private boolean serving = false; // default
 
    QueueEntry(int number, Type type) {
        this.number = number;
        this.type = type;
    }
    
    public int getNumber() {
        return number;
    }

        public String getNumberText() {
        return String.format("%02d", number);
    }
 
    public Type getType() {
        return type;
    }
 
    public boolean isServing() {
        return serving;
    }
    
    public String getStatus() {
        return serving ? "Serving" : "Queued";
    }
 
    void setServing(boolean serving) {
        this.serving = serving;
    }

}
