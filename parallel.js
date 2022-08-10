#!/usr/bin/env node
const https = require("https");
const { URL } = require("url");

const data = JSON.stringify({ name: "Test", price: 2200 });
const [countArg, url] = process.argv.slice(2);

const count = parseInt(countArg);

if (count == NaN) {
    throw new Error("First argument must be number");
}

if (!url) {
    throw new Error("Second argument must be url");
}

const { hostname, pathname: path } = new URL(url);

const options = {
    hostname,
    port: 443,
    path,
    method: "PUT",
    headers: {
        "Content-Type": "application/json",
        "Content-Length": data.length,
    },
};

const doPost = () =>
    new Promise((resolve, reject) => {
        const req = https.request(options, (res) => {
            res.on("data", (d) => {
                //process.stdout.write(d);
            });
            res.on("end", resolve);
        });
        req.on("error", reject);
        req.write(data);
        req.end();
    });

(async () => {
    const start = new Date().getTime();
    console.log(`Executing ${count} requests...`);
    await Promise.all(
        new Array(count).fill(0).map(async (_, index) => {
            const start = new Date().getTime();
            try {
                const res = await doPost();
                const end = new Date().getTime();
                console.log(`Done with ${index + 1} in ${end - start}ms`);
            } catch (error) {
                console.error(error);
            }
        })
    );
    const end = new Date().getTime();
    console.log(`========\nAll done in ${end - start}ms`);
})().catch(console.error);
