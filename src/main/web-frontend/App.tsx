import {
  createBrowserRouter, RouterProvider
} from "react-router";
import HomePage from "pages/HomePage";
import {createRelayEnvironment, RelayRoot} from "@spa-kit/react-relay";
import {renderComponent} from "@spa-kit/react";
import AboutPage from "./pages/AboutPage";
import BlogPage from "./pages/BlogPage";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import CsrfUtils from "./utils/CsrfUtils";
import './styles.css';

const router = createBrowserRouter([
  {
    path: '/',
    element: <HomePage />
  },
  {
    path: '/login',
    element: <LoginPage />
  },
  {
    path: '/register',
    element: <RegisterPage />
  },
  {
    path: '/about',
    element: <AboutPage />
  },
  {
    path: '/blog',
    element: <BlogPage />
  }
])

const environment = createRelayEnvironment({
  headers: () => ({ [CsrfUtils.getHeader()]: CsrfUtils.getToken() }),
});

export function App() {
  return (
    <RelayRoot environment={environment} fallback={null}>
      <RouterProvider router={router} />
    </RelayRoot>
  );
}

renderComponent(<App />)
