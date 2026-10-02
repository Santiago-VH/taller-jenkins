import { useEffect, useState } from 'react'
import { Task, fetchTasks, createTask, completeTask, deleteTask } from './api'

export default function App() {
  const [tasks, setTasks] = useState<Task[]>([])
  const [title, setTitle] = useState('')
  const [subject, setSubject] = useState('')
  const [error, setError] = useState<string | null>(null)

  const loadTasks = async () => {
    try {
      setTasks(await fetchTasks())
      setError(null)
    } catch (e) {
      setError('No fue posible conectar con el backend (studytrack-api).')
    }
  }

  useEffect(() => {
    loadTasks()
  }, [])

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!title.trim()) return
    await createTask({ title, subject, dueDate: new Date().toISOString().slice(0, 10) })
    setTitle('')
    setSubject('')
    loadTasks()
  }

  return (
    <div style={{ maxWidth: 640, margin: '2rem auto', fontFamily: 'sans-serif' }}>
      <h1>StudyTrack</h1>
      <p>Seguimiento de tareas académicas — Icesi EduTech</p>

      {error && <p style={{ color: 'crimson' }}>{error}</p>}

      <form onSubmit={handleCreate} style={{ marginBottom: '1.5rem' }}>
        <input
          placeholder="Título de la tarea"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
        />
        <input
          placeholder="Materia"
          value={subject}
          onChange={(e) => setSubject(e.target.value)}
        />
        <button type="submit">Agregar</button>
      </form>

      <ul>
        {tasks.map((t) => (
          <li key={t.id}>
            <span style={{ textDecoration: t.completed ? 'line-through' : 'none' }}>
              {t.title} ({t.subject})
            </span>{' '}
            {!t.completed && (
              <button onClick={() => t.id && completeTask(t.id).then(loadTasks)}>
                Completar
              </button>
            )}
            <button onClick={() => t.id && deleteTask(t.id).then(loadTasks)}>Eliminar</button>
          </li>
        ))}
      </ul>
    </div>
  )
}
