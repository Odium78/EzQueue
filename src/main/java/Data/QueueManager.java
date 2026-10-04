/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Data;

/**
 *
 * @author lans
 */

import Data.QueueEntry.Lane;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class QueueManager {
    public interface Listener { void queueChanged(); }

    private static final QueueManager INSTANCE = new QueueManager();
    
    public static QueueManager getInstance() { return INSTANCE; }
    
    private final List<QueueEntry> regular = new ArrayList<>();
    private final List<QueueEntry> priority = new ArrayList<>();
    private int nextNumber = 1;
    private final List<Listener> listeners = new ArrayList<>();
    
    private QueueManager() {}
    
    private List<QueueEntry> line(Lane lane) { return lane == Lane.PRIORITY ? priority : regular; }
    
    public QueueEntry add(QueueEntry.Type type) {
        QueueEntry entry = new QueueEntry(nextNumber++, type);
        line(type.getLane()).add(entry);
        fireChanged();
        return entry;
    }

    
    public boolean remove(QueueEntry entry) {
        if (entry == null) return false;
        boolean removed = line(entry.getType().getLane()).remove(entry);
        if (removed) fireChanged();
        return removed;
    }

    public boolean serve(QueueEntry entry) {
        if (entry == null) return false;
        List<QueueEntry> line = line(entry.getType().getLane());
        if (!line.contains(entry)) return false;
        if (entry.isServing()) return true; // already serving, nothing to do
 
        for (QueueEntry other : line) {
            other.setServing(false);
        }
        entry.setServing(true);
        fireChanged();
        return true;
    }
    
    public QueueEntry getHead(Lane lane) {
        List<QueueEntry> line = line(lane);
        return line.isEmpty() ? null : line.get(0);
    }
    
    public QueueEntry getRear(Lane lane) {
        List<QueueEntry> line = line(lane);
        return line.isEmpty() ? null : line.get(line.size() - 1);
    }

    public QueueEntry getServing(Lane lane) {
        for (QueueEntry entry : line(lane)) {
            if (entry.isServing()) return entry;
        }
        return null;
    }
    
    public List<QueueEntry> getLine(Lane lane) {
        return new ArrayList<>(line(lane));
    }

    public List<QueueEntry> getAll() {
        List<QueueEntry> all = new ArrayList<>(regular);
        all.addAll(priority);
        all.sort(Comparator.comparingInt(QueueEntry::getNumber));
        return all;
    }

    // remote events
    public void addListener(Listener listener) {
        listeners.add(listener);
    }
 
    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }
 
    private void fireChanged() {
        for (Listener listener : new ArrayList<>(listeners)) {
            listener.queueChanged();
        }
    }
}
