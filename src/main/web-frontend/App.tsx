import {
  RouterProvider, type RouteObject,
} from "react-router";
import HomePage from "pages/HomePage";
import {createRelayEnvironment, RelayRoot} from "@spa-kit/react-relay";
import {renderComponent} from "@spa-kit/react";
import {createSpaRoutingBrowserRouter} from "@spa-kit/react-router";
import AboutPage from "./pages/AboutPage";
import BlogPage from "./pages/BlogPage";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import CsrfUtils from "./utils/CsrfUtils";
import {AppRoutes} from "routes/AppRoutes";

const routes: RouteObject[] = [
  {
    id: AppRoutes.Home.routeId,
    path: AppRoutes.Home.path,
    element: <HomePage />
  },
  {
    id: AppRoutes.Login.routeId,
    path: AppRoutes.Login.path,
    element: <LoginPage />
  },
  {
    id: AppRoutes.Register.routeId,
    path: AppRoutes.Register.path,
    element: <RegisterPage />
  },
  {
    id: AppRoutes.About.routeId,
    path: AppRoutes.About.path,
    element: <AboutPage />
  },
  {
    id: AppRoutes.Blog.routeId,
    path: AppRoutes.Blog.path,
    element: <BlogPage />
  }
]

const router = createSpaRoutingBrowserRouter(routes, {
  applicationId: AppRoutes.Home.applicationId,
  onError: { type: "allow" },
});

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
