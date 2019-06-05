[
    {
        "outputs": [
            {
                "name": "mintedBy",
                "type": "address"
            },
            {
                "name": "mintedAt",
                "type": "uint128"
            },
            {
                "name": "email",
                "type": "bytes16"
            },
            {
                "name": "name",
                "type": "bytes16"
            },
            {
                "name": "ID",
                "type": "bytes16"
            },
            {
                "name": "status",
                "type": "uint8"
            },
            {
                "name": "description",
                "type": "bytes16"
            }
        ],
        "constant": false,
        "payable": false,
        "inputs": [
            {
                "name": "_tokenId",
                "type": "bytes16"
            }
        ],
        "name": "getToken",
        "type": "function"
    },
    {
        "outputs": [
            {
                "name": "unknown",
                "type": "address"
            }
        ],
        "constant": "true",
        "payable": "false",
        "inputs": [
            {
                "name": "unknown",
                "type": "bytes16"
            }
        ],
        "name": "tokenIndexToApproved",
        "type": "function"
    },
    {
        "outputs": [
            {
                "name": "tokenID",
                "type": "uint128"
            },
            {
                "name": "time",
                "type": "uint128"
            },
            {
                "name": "user",
                "type": "address"
            }
        ],
        "constant": false,
        "payable": false,
        "inputs": [
            {
                "name": "_email",
                "type": "bytes16"
            },
            {
                "name": "_name",
                "type": "bytes16"
            },
            {
                "name": "_ideaID",
                "type": "bytes16"
            },
            {
                "name": "_status",
                "type": "uint8"
            },
            {
                "name": "_description",
                "type": "bytes16"
            },
            {
                "name": "_tokenId",
                "type": "bytes16"
            }
        ],
        "name": "minting",
        "type": "function"
    },
    {
        "outputs": [
            {
                "name": "unknown",
                "type": "address"
            }
        ],
        "constant": true,
        "payable": false,
        "inputs": [
            {
                "name": "unknown",
                "type": "bytes16"
            }
        ],
        "name": "tokenIndexToOwner",
        "type": "function"
    },
    {
        "outputs": [],
        "inputs": [
            {
                "indexed": false,
                "name": "owner",
                "type": "address"
            },
            {
                "indexed": false,
                "name": "videoID",
                "type": "uint128"
            }
        ],
        "name": "Mint",
        "anonymous": "false",
        "type": "event"
    },
    {
        "outputs": [],
        "inputs": [
            {
                "indexed": false,
                "name": "from",
                "type": "address"
            },
            {
                "indexed": false,
                "name": "to",
                "type": "address"
            },
            {
                "indexed": false,
                "name": "tokenId",
                "type": "bytes16"
            }
        ],
        "name": "Transfer",
        "anonymous": false,
        "type": "event"
    },
    {
        "outputs": [],
        "inputs": [
            {
                "indexed": false,
                "name": "owner",
                "type": "address"
            },
            {
                "indexed": "false",
                "name": "approved",
                "type": "address"
            },
            {
                "indexed": false,
                "name": "tokenId",
                "type": "bytes16"
            }
        ],
        "name": "Approval",
        "anonymous": false,
        "type": "event"
    }
]