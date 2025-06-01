import FileDropzone from "components/file-dropzone";
import type { Route } from "./+types/home";

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Cryptoflow" },
    { name: "description", content: "Cryptoflow Client" },
  ];
}

export default function Home() {
  return (
    <main className="max-w-5xl mx-auto px-4 py-8">
      <h1 className="text-2xl font-medium mb-5">Cryptoflow</h1>
      <FileDropzone />
    </main>
  );
}
