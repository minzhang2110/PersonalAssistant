package com.zm.PersonalAssistant.Reminder;

import com.zm.PersonalAssistant.utils.LunarCalendar;
import static com.zm.PersonalAssistant.utils.Log.log;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by zhangmin on 2016/6/23.
 */
public class ReminderMgr implements Serializable {
    private List<Reminder> list = new ArrayList<>();
    private static final ReminderMgr instance = new ReminderMgr();

    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = readWriteLock.readLock();
    private final Lock writeLock = readWriteLock.writeLock();

    private ReminderMgr(){}

    public String toString(List<Reminder> listToDisplay){
        StringBuilder ret = new StringBuilder();
        Reminder reminder;
        readLock.lock();
        for(int i = 0; i < listToDisplay.size(); i++) {
            reminder = listToDisplay.get(i);
            ret.append(i).append(". ").append(reminder).append("\r\n");
        }
        readLock.unlock();
        return ret.toString();
    }

    public void add(Reminder reminder){
        writeLock.lock();
        list.add(reminder);
        log.info("Add a reminder : " + reminder);
        sort();
        writeLock.unlock();
    }

    public String getNotify(LunarCalendar timeNow){
        StringBuilder ret = new StringBuilder();
        String aNotifyStr = "";
        writeLock.lock();
        //遍历，删除过期的reminder，用迭代来删除以免出现问题
        Iterator<Reminder> reminderIterator = list.iterator();
        while (reminderIterator.hasNext()){
            Reminder reminder = reminderIterator.next();
            aNotifyStr = reminder.getNotify(timeNow);
            if(!aNotifyStr.equals("")){
                ret.append(aNotifyStr);
                ret.append("\r\n");
            }
            if(reminder.isDeletable()){
                reminderIterator.remove();
                log.info("Remove a expired reminder : " + reminder);
            }
        }
        sort();
        writeLock.unlock();
        log.debug("Get notifies : " + ret.toString());
        return ret.toString();
    }

    private void sort(){
        Collections.sort(list);
    }

    public static ReminderMgr getInstance() {
        return instance;
    }

    //for test
    protected List<Reminder> getAllReminders() {
        return list;
    }

    public void removeAccordingToNumber(int num){
        writeLock.lock();
        boolean found = false;
        for(Reminder reminder : list){
            if(reminder.getNumber() == num){
                found = true;
                list.remove(reminder);
                log.info("Remove a reminder according to num " + num + " : " + reminder);
                break;
            }
        }
        if(!found){
            log.error("Remove reminder fail : Can not find a reminder whose number is " + num);
            throw new IllegalArgumentException("Remove reminder fail : Can not find a reminder whose number is " + num);
        }
        writeLock.unlock();
    }

    public String getReminderStrNextDays(LunarCalendar timeNow, int days){
        List<Reminder> remindersInDays = new ArrayList<>();
        LunarCalendar sevenDayLater = new LunarCalendar(false, timeNow.getYear(), timeNow.getMonth(), timeNow.getDate());
        sevenDayLater.addDate(days);
        sevenDayLater.addMinute(-1);
        readLock.lock();
        for(Reminder reminder : this.list) {
            if(sevenDayLater.compareTo(reminder.getRemindTime()) >= 0 && timeNow.compareTo(reminder.getRemindTime()) <= 0){
                remindersInDays.add(reminder);
            }
        }
        String ret = toString(remindersInDays);
        readLock.unlock();
        log.debug("Get reminders in next " + days + "days : " + ret);
        return ret;
    }

    public String getReminderStrCount(int count){
        readLock.lock();
        String ret = toString(this.list.subList(0, count));
        readLock.unlock();
        log.debug("Get next " + count + " reminders: " + ret);
        return ret;
    }

    public String getAllReminderStr(){
        readLock.lock();
        String ret = toString(this.list);
        readLock.unlock();
        log.debug("Get all reminders: " + ret);
        return ret;
    }

    //解决单例模式序列化问题，保证反序列化后还是指向一个对象
    private Object readResolve() {
        return instance;
    }
}
