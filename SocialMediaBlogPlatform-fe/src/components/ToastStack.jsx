export function ToastStack({ toasts, onDismiss }) {
  if (!toasts.length) {
    return null
  }

  return (
    <div aria-live="polite" className="toast-stack">
      {toasts.map((toast) => (
        <section className={`toast-card toast-${toast.type || 'error'}`} key={toast.id}>
          <div>
            <strong>{toast.title || 'Something needs attention'}</strong>
            <p>{toast.message}</p>
          </div>
          <button aria-label="Dismiss notification" type="button" onClick={() => onDismiss(toast.id)}>
            x
          </button>
        </section>
      ))}
    </div>
  )
}
