import { fetcher } from "@/utils/fetcher";
import { twx } from "@/utils/twx";
import { DownloadCloudIcon, Loader2Icon } from "lucide-react";
import useSWR from "swr";

type ProcessedImage = {
  id: string;
  imageName: string;
  imageData: string;
  encryptionMode: string;
  operation: string;
};

export default function ProcessedList() {
  const { data, error, isLoading } = useSWR<ProcessedImage[]>(
    "http://localhost:8080/images/processed",
    fetcher,
    { refreshInterval: 1000 }
  );

  if (isLoading)
    return <Loader2Icon className="animate-spin h-6 w-6 text-gray-500" />;
  if (error)
    return (
      <div className="text-red-800 py-2 px-4 bg-red-100 rounded-lg">
        Failed to load processed images
      </div>
    );

  if (!data || data.length === 0) {
    return (
      <div className="text-gray-500 py-2 px-4 bg-gray-100 rounded-lg">
        No processed images found
      </div>
    );
  }

  return (
    <div className="space-y-2">
      <h2 className="text-lg font-semibold mb-4">Processed Images</h2>
      {data.map((image) => (
        <div
          key={image.id}
          className="flex items-center justify-between gap-4 p-4 border-b"
        >
          <div>
            <div className="flex items-center gap-2">
              <div className="w-2 h-2 bg-green-500 rounded-full"></div>
              <p>{image.imageName}</p>
              <div
                className={twx(
                  "text-xs text-gray-500",
                  image.operation === "ENCRYPT"
                    ? "text-violet-500"
                    : "text-fuchsia-500"
                )}
              >
                {image.operation}
              </div>
              <div className="text-xs text-gray-500">
                {image.encryptionMode.split("_")[1]}
              </div>
            </div>
            <span className="text-gray-800 text-xs">{image.id}</span>
          </div>
          <div>
            <DownloadCloudIcon
              className="w-6 h-6 text-neutral-500 cursor-pointer hover:text-neutral-600 transition-colors"
              onClick={() => {
                const binaryString = atob(image.imageData);
                const binaryArray = new Uint8Array(binaryString.length);
                for (let i = 0; i < binaryString.length; i++) {
                  binaryArray[i] = binaryString.charCodeAt(i);
                }
                const blob = new Blob([binaryArray], { type: "image/bmp" });

                const link = document.createElement("a");
                link.href = URL.createObjectURL(blob);
                link.download = image.imageName;
                document.body.appendChild(link);

                link.click();
                document.body.removeChild(link);
              }}
            />
          </div>
        </div>
      ))}
    </div>
  );
}
