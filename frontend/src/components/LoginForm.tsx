import { useState, type FormEvent } from "react";
import {login, type UserRole} from "../api";

interface LoginFormProps {
    onLogin: (token: string, role: UserRole) => void;
}

export default function LoginForm({ onLogin }: LoginFormProps) {

    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    async function handleSubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault();

        try {
            setLoading(true);
            setError(null);

            const response = await login(username, password);

            onLogin(response.token, response.role);

        } catch (err) {
            setError(
                err instanceof Error ? err.message : "Login failed"
            );
        } finally {
            setLoading(false);
        }
    }

    return (
        <div>
            <h2>Login to ParcelFlow</h2>

            <form onSubmit={handleSubmit}>

                <div>
                    <label htmlFor="username">Username</label>
                    <input
                        id="username"
                        type="text"
                        value={username}
                        onChange={(event) => setUsername(event.target.value)}
                        required
                    />
                </div>

                <div>
                    <label htmlFor="password">Password</label>
                    <input
                        id="password"
                        type="password"
                        value={password}
                        onChange={(event) => setPassword(event.target.value)}
                        required
                    />
                </div>

                {error && <p style={{ color: "red" }}>{error}</p>}

                <button type="submit" disabled={loading}>
                    {loading ? "Logging in..." : "Login"}
                </button>

            </form>
        </div>
    );
}