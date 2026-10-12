import { useState, type FormEvent } from "react";
import { createShipment } from "../api";

interface Props {
    onCreated: () => void;
    token: string;
}

export default function CreateShipmentForm({
                                               onCreated,
                                               token
                                           }: Props) {

    const [senderName, setSenderName] = useState("");
    const [recipientName, setRecipientName] = useState("");
    const [origin, setOrigin] = useState("");
    const [destination, setDestination] = useState("");

    async function handleSubmit(
        event: FormEvent<HTMLFormElement>
    ) {
        event.preventDefault();

        try {
            await createShipment(token, {
                senderName,
                recipientName,
                origin,
                destination,
            });

            setSenderName("");
            setRecipientName("");
            setOrigin("");
            setDestination("");

            onCreated();

        } catch (error) {
            console.error(error);
            alert("Could not create shipment");
        }
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