import { Component, type ErrorInfo, type ReactNode } from "react";
import { btnPrimary } from "./ui";

interface Props {
  children: ReactNode;
}

interface State {
  error: Error | null;
}

export class ErrorBoundary extends Component<Props, State> {
  state: State = { error: null };

  static getDerivedStateFromError(error: Error): State {
    return { error };
  }

  componentDidCatch(error: Error, info: ErrorInfo) {
    console.error("Error no controlado en la UI:", error, info.componentStack);
  }

  render() {
    const { error } = this.state;
    if (!error) return this.props.children;

    return (
      <div className="rounded-lg border border-red-200 bg-red-50 p-6 text-center dark:border-red-900 dark:bg-red-950/40">
        <h2 className="mb-2 text-lg font-semibold text-red-800 dark:text-red-300">
          Algo salió mal en esta pantalla
        </h2>
        <p className="mb-4 text-sm text-red-700 dark:text-red-400">
          {error.message}
        </p>
        <button
          className={btnPrimary}
          onClick={() => this.setState({ error: null })}
        >
          Reintentar
        </button>
      </div>
    );
  }
}
