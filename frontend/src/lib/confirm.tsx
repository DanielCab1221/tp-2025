import {
  createContext,
  useCallback,
  useContext,
  useState,
  type ReactNode,
} from "react";
import { Modal, btnDanger, btnSecondary } from "../components/ui";

interface ConfirmOptions {
  title?: string;
  confirmLabel?: string;
}

type ConfirmFn = (
  message: string,
  options?: ConfirmOptions,
) => Promise<boolean>;

interface PendingConfirm extends Required<ConfirmOptions> {
  message: string;
  resolve: (value: boolean) => void;
}

const ConfirmContext = createContext<ConfirmFn | null>(null);

export function ConfirmProvider({ children }: { children: ReactNode }) {
  const [pending, setPending] = useState<PendingConfirm | null>(null);

  const confirm = useCallback<ConfirmFn>(
    (message, options) =>
      new Promise<boolean>((resolve) => {
        setPending({
          message,
          title: options?.title ?? "Confirmar acción",
          confirmLabel: options?.confirmLabel ?? "Confirmar",
          resolve,
        });
      }),
    [],
  );

  function respond(value: boolean) {
    pending?.resolve(value);
    setPending(null);
  }

  return (
    <ConfirmContext.Provider value={confirm}>
      {children}
      <Modal
        open={pending !== null}
        onClose={() => respond(false)}
        title={pending?.title ?? ""}
      >
        <p className="mb-4 text-sm text-gray-700 dark:text-gray-300">
          {pending?.message}
        </p>
        <div className="flex justify-end gap-2">
          <button className={btnSecondary} onClick={() => respond(false)}>
            Cancelar
          </button>
          <button className={btnDanger} onClick={() => respond(true)}>
            {pending?.confirmLabel}
          </button>
        </div>
      </Modal>
    </ConfirmContext.Provider>
  );
}

export function useConfirm() {
  const ctx = useContext(ConfirmContext);
  if (!ctx)
    throw new Error("useConfirm debe usarse dentro de <ConfirmProvider>");
  return ctx;
}
