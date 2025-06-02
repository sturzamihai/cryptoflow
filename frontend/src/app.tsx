import FileDropzone from "@/components/file-dropzone";
import ProcessedList from "./components/processed-list";

function App() {
  return (
    <main className="max-w-5xl mx-auto px-4 py-8">
      <h1 className="text-2xl font-medium mb-5">Cryptoflow</h1>
      <div>
      <FileDropzone />
      </div>
      <div className="mt-8">
      <ProcessedList />
      </div>
    </main>
  );
}

export default App;
