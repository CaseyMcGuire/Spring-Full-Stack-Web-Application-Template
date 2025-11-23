import * as React from "react";
import {RelayConfig} from "./relay/RelayConfig";
import {
  createBrowserRouter, RouterProvider
} from "react-router";
import HomePage from "pages/HomePage";
import {RelayEnvironmentProvider} from "react-relay/hooks";
import AboutPage from "./pages/AboutPage";
import BlogPage from "./pages/BlogPage";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import {renderComponent} from "./utils/ReactPageUtils";

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

export function App() {
  return (
    <RelayEnvironmentProvider environment={RelayConfig.getEnvironment()}>
      <React.Suspense fallback={null}>
        <RouterProvider router={router} />
      </React.Suspense>
    </RelayEnvironmentProvider>
  );
}

renderComponent(<App />)
