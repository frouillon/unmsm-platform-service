import { createBrowserRouter } from 'react-router'
import { NotFoundPage } from '../pages/NotFoundPage'
import { PlaceholderPage } from '../pages/PlaceholderPage'

export const router = createBrowserRouter([
  { path: '/', element: <PlaceholderPage /> },
  { path: '*', element: <NotFoundPage /> },
])
