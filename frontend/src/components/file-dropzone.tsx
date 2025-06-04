import {
  CheckIcon,
  ChevronDownIcon,
  FileIcon,
  FunctionSquareIcon,
  KeyRoundIcon,
  Loader2Icon,
  LockIcon,
  RefreshCwIcon,
  UnlockIcon,
  UploadCloudIcon,
} from "lucide-react";
import { useEffect, useState } from "react";
import { useDropzone } from "react-dropzone";

export default function FileDropzone() {
  const [file, setFile] = useState<File | null>(null);
  const [key, setKey] = useState<string>("");
  const [mode, setMode] = useState<string>("ECB");
  const [error, setError] = useState<string | null>(null);
  const [state, setState] = useState<
    "unsubmitted" | "processing" | "completed"
  >("unsubmitted");

  const dropzone = useDropzone({
    accept: {
      "image/bmp": [".bmp"],
    },
    onDrop: (acceptedFiles) => {
      if (acceptedFiles.length > 0) {
        const file = acceptedFiles[0];
        setFile(file);
      } else {
        setError("No valid BMP file selected");
        setFile(null);
      }
    },
    onDropRejected: (rejectedFiles) => {
      const errorMessage = rejectedFiles
        .map((file) => file.errors.map((error) => error.message).join(", "))
        .join(", ");

      setError(errorMessage);
      setFile(null);
    },
    multiple: false,
    maxFiles: 1,
  });

  useEffect(() => {
    setKey("");
  }, [file]);

  useEffect(() => {
    setError(null);
  }, [file, key]);

  const handleOperation = async (operation: "encrypt" | "decrypt") => {
    setState("processing");
    if (!file) {
      setError("No file selected");
      setState("unsubmitted");
      return;
    }

    if (!key || key.trim() === "") {
      setError("Please enter a valid encryption key");
      setState("unsubmitted");
      return;
    }

    if(key.length < 16 || key.length > 32) {
      setError("Key must be between 16 and 32 characters long");
      setState("unsubmitted");
      return;
    }

    const formData = new FormData();
    formData.append("file", file);
    formData.append("key", key);
    formData.append("mode", mode);

    const response = await fetch(`http://localhost:8080/images/${operation}`, {
      method: "POST",
      body: formData,
    });

    const data = await response.json();

    if (!response.ok) {
      setError(`${data.error || "Unknown error"}`);
      setState("unsubmitted");
      return;
    }

    setState("completed");
  };

  if (file) {
    return (
      <>
        <div className="bg-neutral-100 dark:bg-neutral-900 rounded-xl p-4 flex gap-4 justify-between flex-wrap">
          <div>
            <div className="text-neutral-700 dark:text-neutral-300 flex items-center gap-2">
              <FileIcon className="w-4 h-4" /> {file.name}
            </div>
            <div className="flex items-center gap-4">
              <div className="flex items-center gap-2">
                <KeyRoundIcon className="w-4 h-4" />
                <input
                  type="text"
                  placeholder="Encryption key"
                  value={key}
                  onChange={(e) => setKey(e.target.value)}
                />
              </div>
              <div className="flex items-center">
                <FunctionSquareIcon className="w-4 h-4 mr-2" />
                <select
                  className="appearance-none bg-none border-0"
                  value={mode}
                  onChange={(e) => setMode(e.target.value)}
                >
                  <option value="ECB">ECB</option>
                  <option value="CBC">CBC</option>
                </select>
                <ChevronDownIcon className="w-4 h-4 text-neutral-500" />
              </div>
            </div>
          </div>
          {state === "completed" && (
            <div className="flex items-center gap-4">
              <div className="flex items-center gap-2 bg-green-100 text-green-800 px-4 py-2 rounded-full">
                <CheckIcon className="w-4 h-4" />
                <span>Success</span>
              </div>
              <div className="p-2 bg-neutral-200 dark:bg-neutral-800 rounded-full">
                <RefreshCwIcon
                  className="w-4 h-4 text-neutral-500 rounded-full cursor-pointer hover:text-neutral-700 transition-all hover:rotate-45"
                  onClick={() => {
                    setFile(null);
                    setState("unsubmitted");
                    setKey("");
                    setMode("ECB");
                  }}
                />
              </div>
            </div>
          )}
          {state === "processing" && (
            <div className="flex items-center justify-center gap-2">
              <span className="text-neutral-500">Processing...</span>
              <Loader2Icon className="animate-spin h-6 w-6" />
            </div>
          )}
          {state === "unsubmitted" && (
            <div className="flex items-center gap-4">
              <div
                onClick={() => handleOperation("encrypt")}
                className="flex text-center items-center justify-center gap-2 bg-violet-500 text-white hover:cursor-pointer hover:bg-violet-600 transition-all font-medium py-2 px-4 rounded-full"
              >
                <LockIcon className="w-4 h-4" />
                Encrypt
              </div>
              <div
                onClick={() => handleOperation("decrypt")}
                className="flex text-center items-center justify-center gap-2 bg-fuchsia-500 text-white hover:cursor-pointer hover:bg-fuchsia-600 transition-all font-medium py-2 px-4 rounded-full"
              >
                <UnlockIcon className="w-4 h-4" />
                Decrypt
              </div>
            </div>
          )}
        </div>
        {error && <div className="text-red-500 mt-2 text-sm">{error}</div>}
      </>
    );
  }

  return (
    <>
      <div
        {...dropzone.getRootProps({
          className:
            "dropzone h-[200px] bg-neutral-100 dark:bg-neutral-900 rounded-xl flex flex-col items-center justify-center",
        })}
      >
        <input {...dropzone.getInputProps()} />
        <UploadCloudIcon className="h-12 w-12 text-neutral-500 mb-2" />
        <p className="text-neutral-500">
          Drag and drop, or click to select BMP files
        </p>
      </div>
      {error && <div className="text-red-500 mt-2 text-sm">{error}</div>}
    </>
  );
}
