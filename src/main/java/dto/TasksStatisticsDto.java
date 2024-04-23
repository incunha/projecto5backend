package dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;

public class TasksStatisticsDto {
    private int totalTasks;
    private int totaDoneTasks;
    private int totalDoingTasks;
    private int totalToDoTasks;
    private int averageTaskTime;
    private double averageTasksPerUser;
    private HashMap<LocalDate,Long> tasksCompletedByDate;
    private HashMap<String,Long> tasksByCategory;


    public TasksStatisticsDto() {
    }

    public int getTotalTasks() {
        return totalTasks;
    }

    public void setTotalTasks(int totalTasks) {
        this.totalTasks = totalTasks;
    }

    public int getTotaDoneTasks() {
        return totaDoneTasks;
    }

    public void setTotaDoneTasks(int totaDoneTasks) {
        this.totaDoneTasks = totaDoneTasks;
    }

    public int getTotalDoingTasks() {
        return totalDoingTasks;
    }

    public void setTotalDoingTasks(int totalDoingTasks) {
        this.totalDoingTasks = totalDoingTasks;
    }

    public int getTotalToDoTasks() {
        return totalToDoTasks;
    }

    public void setTotalToDoTasks(int totalToDoTasks) {
        this.totalToDoTasks = totalToDoTasks;
    }

    public int getAverageTaskTime() {
        return averageTaskTime;
    }

    public void setAverageTaskTime(int averageTaskTime) {
        this.averageTaskTime = averageTaskTime;
    }

    public double getAverageTasksPerUser() {
        return averageTasksPerUser;
    }

    public void setAverageTasksPerUser(double averageTasksPerUser) {
        this.averageTasksPerUser = averageTasksPerUser;
    }

    public HashMap<LocalDate, Long> getTasksCompletedByDate() {
        return tasksCompletedByDate;
    }

    public void setTasksCompletedByDate(HashMap<LocalDate, Long> tasksCompletedByDate) {
        this.tasksCompletedByDate = tasksCompletedByDate;
    }

    public HashMap<String, Long> getTasksByCategory() {
        return tasksByCategory;
    }

    public void setTasksByCategory(HashMap<String, Long> tasksByCategory) {
        this.tasksByCategory = tasksByCategory;
    }
}