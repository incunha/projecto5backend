package dto;

public class TaskWebsocketDto {

    private String taskId;

    private Task task;

    public TaskWebsocketDto() {
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }
}

