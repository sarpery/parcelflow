import { useState } from "react";

interface Props {
    onCreated: () => void;
}

export default function CreateShipmentForm({ onCreated }: Props) {

    const [senderName, setSenderName] = useState("");
    const [recipientName, setRecipientName] = useState("");
    const [origin, setOrigin] = useState("");
    const [destination, setDestination] = useState("");

    async function handleSubmit(
        event: React.FormEvent<HTMLFormElement>
    ) {
        event.preventDefault();

        const response = await fetch(
            "http://localhost:8080/api/shipments",
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({
                    senderName,
                    recipientName,
                    origin,
                    destination,
                }),
            }
        );

        if (!response.ok) {
            alert("Could not create shipment");
            return;
        }

        setSenderName("");
        setRecipientName("");
        setOrigin("");
        setDestination("");

        onCreated();
    }

    return (
        <form onSubmit={handleSubmit}>

            <input
                placeholder="Sender"
                value={senderName}
                onChange={(e) => setSenderName(e.target.value)}
            />

            <input
                placeholder="Recipient"
                value={recipientName}
                onChange={(e) => setRecipientName(e.target.value)}
            />

            <input
                placeholder="Origin"
                value={origin}
                onChange={(e) => setOrigin(e.target.value)}
            />

            <input
                placeholder="Destination"
                value={destination}
                onChange={(e) => setDestination(e.target.value)}
            />

            <button type="submit">
                Create Shipment
            </button>

        </form>
    );
}