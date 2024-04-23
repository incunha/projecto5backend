package dto;

public class TaskWebsocketDto {

    private String action;

    private Task task;

    public TaskWebsocketDto(String action) {
        this.action = action;

    }

    public TaskWebsocketDto() {
        this.action = action;
        this.task = task;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }
}

