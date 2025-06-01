import {
  DatabaseIcon,
  FileIcon,
  KeyRoundIcon,
  LockIcon,
  UnlockIcon,
  UploadCloudIcon,
} from "lucide-react";
import { useEffect, useState } from "react";
import { useDropzone } from "react-dropzone";

export default function FileDropzone() {
  const [file, setFile] = useState<File | null>(null);
  const [key, setKey] = useState<string>("");
  const [error, setError] = useState<string | null>(null);

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
    if (!file) {
      setError("No file selected");
      return;
    }

    if (!key || key.trim() === "") {
      setError("Please enter a valid encryption key");
      return;
    }

    const formData = new FormData();
    formData.append("file", file);
    formData.append("key", key);
    formData.append("mode", "ECB"); // TODO: Make this configurable in the future

    const response = await fetch(`http://localhost:8080/images/${operation}`, {
      method: "POST",
      body: formData,
    });

    const data = await response.json();

    if (!response.ok) {
      setError(`${data.message || "Unknown error"}`);
      return;
    }

    
  };

  if (file) {
    return (
      <>
        <div className="bg-neutral-100 dark:bg-neutral-900 rounded-xl p-4 flex gap-4 justify-between flex-wrap">
          <div>
            <div className="text-neutral-700 dark:text-neutral-300 flex items-center gap-2">
              <FileIcon className="w-4 h-4" /> {file.name}
            </div>
            <div className="flex items-center gap-2">
              <KeyRoundIcon className="w-4 h-4" />
              <input
                type="text"
                placeholder="Encryption key"
                value={key}
                onChange={(e) => setKey(e.target.value)}
              />
            </div>
          </div>
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
