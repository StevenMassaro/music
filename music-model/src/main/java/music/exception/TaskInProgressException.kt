package music.exception

/**
 * Thrown if a task is requested but that task is already executing. Pass in a [taskName], like "sync" or "migration".
 */
class TaskInProgressException(taskName: String) : Exception("A ${taskName.toLowerCase()} is currently in progress. Wait until this ${taskName.toLowerCase()} finishes before starting a new ${taskName.toLowerCase()}.")
