import { createBrowserRouter } from 'react-router'
import { GuestOnly, HomeRedirect, RequireAuth } from '../features/auth/guards'
import { LoginPage } from '../pages/LoginPage'
import { NotFoundPage } from '../pages/NotFoundPage'
import { RoleHomePage } from '../pages/RoleHomePage'

export const router = createBrowserRouter([
  {
    element: <GuestOnly />,
    children: [{ path: '/login', element: <LoginPage /> }],
  },
  {
    element: <RequireAuth />,
    children: [{ path: '/', element: <HomeRedirect /> }],
  },
  { element: <RequireAuth roles={['ESTUDIANTE']} />, children: [{ path: '/inicio', element: <RoleHomePage /> }] },
  { element: <RequireAuth roles={['TECNICO']} />, children: [{ path: '/tecnico', element: <RoleHomePage /> }] },
  { element: <RequireAuth roles={['SUPERVISOR']} />, children: [{ path: '/supervisor', element: <RoleHomePage /> }] },
  { element: <RequireAuth roles={['ADMINISTRADOR']} />, children: [{ path: '/admin', element: <RoleHomePage /> }] },
  { path: '*', element: <NotFoundPage /> },
])
