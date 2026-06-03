import { useEffect, useState } from 'react'

export function useRoute() {
  const currentRoute = () => `${window.location.pathname}${window.location.search}`
  const [route, setRoute] = useState(currentRoute)

  useEffect(() => {
    const onPopState = () => setRoute(currentRoute())
    window.addEventListener('popstate', onPopState)
    return () => window.removeEventListener('popstate', onPopState)
  }, [])

  const navigate = (path) => {
    window.history.pushState({}, '', path)
    setRoute(path)
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  return [route, navigate]
}
