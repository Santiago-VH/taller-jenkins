export interface Task {
  id?: number
  title: string
  subject: string
  dueDate: string
  completed: boolean
}

// La URL del backend se inyecta en tiempo de build vía la variable de
// entorno VITE_API_URL (ver Dockerfile y docker-compose.yml).
const API_URL: string = import.meta.env.VITE_API_URL || 'http://localhost:8080'

export async function fetchTasks(): Promise<Task[]> {
  const res = await fetch(`${API_URL}/api/tasks`)
  if (!res.ok) throw new Error('Error al obtener las tareas')
  return res.json()
}

export async function createTask(task: Omit<Task, 'id' | 'completed'>): Promise<Task> {
  const res = await fetch(`${API_URL}/api/tasks`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(task),
  })
  if (!res.ok) throw new Error('Error al crear la tarea')
  return res.json()
}

export async function completeTask(id: number): Promise<Task> {
  const res = await fetch(`${API_URL}/api/tasks/${id}/complete`, { method: 'PATCH' })
  if (!res.ok) throw new Error('Error al completar la tarea')
  return res.json()
}

export async function deleteTask(id: number): Promise<void> {
  const res = await fetch(`${API_URL}/api/tasks/${id}`, { method: 'DELETE' })
  if (!res.ok) throw new Error('Error al eliminar la tarea')
}
