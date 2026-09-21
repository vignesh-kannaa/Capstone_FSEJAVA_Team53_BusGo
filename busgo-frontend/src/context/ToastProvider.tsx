import { useCallback, useEffect, useMemo, useRef, useState, type ReactNode } from 'react'
import { ToastContext, type ToastContextValue, type ToastKind } from './ToastContext'

interface Toast {
  id: number
  kind: ToastKind
  message: string
}

const TOAST_DURATION_MS = 5000

/** Minimal global toast area. Any feature can show success/error messages with useToast(). */
export function ToastProvider({ children }: { children: ReactNode }) {
  const [toasts, setToasts] = useState<Toast[]>([])
  const nextId = useRef(1)
  const timers = useRef(new Map<number, number>())

  const dismiss = useCallback((id: number) => {
    window.clearTimeout(timers.current.get(id))
    timers.current.delete(id)
    setToasts((current) => current.filter((toast) => toast.id !== id))
  }, [])

  const showToast = useCallback(
    (message: string, kind: ToastKind = 'info') => {
      const id = nextId.current++
      setToasts((current) => [...current, { id, kind, message }])
      timers.current.set(id, window.setTimeout(() => dismiss(id), TOAST_DURATION_MS))
    },
    [dismiss],
  )

  useEffect(() => {
    const activeTimers = timers.current
    return () => activeTimers.forEach((timer) => window.clearTimeout(timer))
  }, [])

  const value = useMemo<ToastContextValue>(() => ({ showToast }), [showToast])

  return (
    <ToastContext.Provider value={value}>
      {children}
      <div className="toast-area" role="status" aria-live="polite">
        {toasts.map((toast) => (
          <div key={toast.id} className={`toast toast-${toast.kind}`}>
            <span>{toast.message}</span>
            <button type="button" className="toast-close" aria-label="Dismiss message" onClick={() => dismiss(toast.id)}>
              ×
            </button>
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  )
}
